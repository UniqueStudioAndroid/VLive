use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};
use std::time::Duration;
use std::fs::File;
use std::io::{BufReader, BufRead, Write};

use chrono::Local;
use std::thread;

use crate::basic::{create_indexes, get_position, rsp_err, rsp_ok};

use super::basic::VLiveResult;
use super::bean::*;
use std::thread::sleep;

lazy_static! {
    static ref MODEL: Mutex<Model> = {
        let mut users = read_users();
        let mut channels = HashMap::new();

        let uid = "8086".to_string();
        let name = "UnityUser".to_string();

        let user = Arc::new(User {
            uid: uid.clone(),
            name: name.clone(),
        });
        users.insert(uid, user.clone());

        let scene = "Eden".to_string();
        let mut indexes = create_indexes(&scene).unwrap();
        let member = ChannelMember {
            user: user,
            mode: 1,
            index: indexes.pop().unwrap(),
        };
        let mut members = HashSet::new();
        members.insert(member);

        let channel = Channel {
            id: name.clone(),
            scene: scene,
            desc: "A place where everyone can play freely".to_string(),
            users: members,
            indexes: indexes,
            last_zero_time: Local::now(),
        };
        channels.insert(name, channel);

        thread::spawn(remove_empty_channel_indefinitely);

        Mutex::new(Model {
            users: users, // users,
            channels: HashMap::new(), // channels,
        })
    };
}

const BASE_USER_SIZE: usize = 10000;

fn read_users() -> HashMap<String, Arc<User>> {
    let mut id = BASE_USER_SIZE;
    let input = File::open("./users.log").unwrap();
    let mut buffered = BufReader::new(input);

    let mut users = HashMap::new();
    let mut done = false;
    let mut count = 0;
    while !done {
        let mut line = String::new();
        match buffered.read_line(&mut line) {
            Ok(0) => done = true,
            Ok(_) => {
                let uid = id.to_string();
                let name = line.trim_end_matches('\n');
                users.insert(uid.clone(), Arc::new(User {
                    uid: uid,
                    name: name.to_string(),
                }));
                println!("Read user name = {}", name);
                id += 1;
            },
            _ => done = true
        };
        count += 1;
        if count > 3 {
            done = true;
        }
        println!("user = {}", line);
    }

    return users;
}

fn write_users(m: &HashMap<String, Arc<User>>) {
    let mut vec: Vec<i32> = m.keys().map(|x| x.parse().unwrap()).collect();
    vec.sort();

    let mut output = File::create("./users.log").unwrap();

    for k in vec {
        if k < 10000 {
            continue;
        }
        m.get(&k.to_string()).and_then(|u| {
            let _ = writeln!(output, "{}", u.name);
            return Some(());
        });
    }
}

pub fn register(data: Vec<u8>) -> VLiveResult {
    let req: UserRegReq = serde_json::from_slice(&data)?;
    let mut model = MODEL.lock().unwrap();
    let uid = (model.users.len() + BASE_USER_SIZE).to_string();
    let user = User {
        uid: uid.clone(),
        name: req.name.trim_end_matches('\n').to_string(),
    };
    println!("Add user: {:?}", &user);

    model.users.insert(uid.clone(), Arc::new(user));
    write_users(&model.users);
    rsp_ok(UserRegRsp { uid })
}

pub fn create_channel(data: Vec<u8>) -> VLiveResult {
    let req: ChannelCreateReq = serde_json::from_slice(&data)?;
    let mut model = MODEL.lock().unwrap();

    let index_set = match create_indexes(&req.scene) {
        Some(v) => v,
        None => return rsp_err("Scene not found"),
    };
    if let Some(_) = model.channels.get(&req.cid) {
        return rsp_err("Channel has existed");
    }

    let cid = req.cid.clone();
    let channel = Channel {
        id: req.cid,
        scene: req.scene,
        desc: req.desc,
        users: HashSet::new(),
        indexes: index_set,
        last_zero_time: Local::now(),
    };

    println!("Create channel {:?}", &channel);
    model.channels.insert(cid, channel);
    rsp_ok(String::new())
}

pub fn join_channel(data: Vec<u8>) -> VLiveResult {
    let req: ChannelJoinReq = serde_json::from_slice(&data)?;
    let mut model = MODEL.lock().unwrap();

    let user = match model.users.get(&req.uid) {
        Some(v) => v,
        None => return rsp_err("User not exist"),
    }
    .clone();

    model.channels.get_mut(&req.cid).map_or_else(
        || rsp_err("Channel not exist"),
        |c| {
            if c.has_user(&user.uid) {
                return rsp_err("Duplicate join");
            }
            let (index, pos) = match get_position(c) {
                Some(p) => p,
                None => return rsp_err("No space available"),
            };
            let rsp = ChannelJoinRsp {
                pos,
                users: c
                    .users
                    .iter()
                    .map(|c| ChannelUserInfo {
                        uid: c.user.uid.clone(),
                        name: c.user.name.clone(),
                        mode: c.mode,
                    })
                    .collect(),
            };
            c.users.insert(ChannelMember::new(user, req, index));
            rsp_ok(rsp)
        },
    )
}

pub fn leave_channel(data: Vec<u8>) -> VLiveResult {
    let req: ChannelLeaveReq = serde_json::from_slice(&data)?;
    let mut model = MODEL.lock().unwrap();

    model.channels.get_mut(&req.cid).map_or_else(
        || rsp_err("Channel not exist"),
        |c| {
            c.remove_user(&req.uid);

            if c.users.is_empty() {
                c.last_zero_time = Local::now();
            }

            rsp_ok(String::new())
        },
    )
}

pub fn list_channel(_: Vec<u8>) -> VLiveResult {
    let model = MODEL.lock().unwrap();
    let mut rsp = Vec::new();

    model.channels.iter().for_each(|c| {
        let c = c.1;
        rsp.push(ChannelListRsp {
            cid: c.id.clone(),
            desc: c.desc.clone(),
            count: c.users.len(),
            max_count: c.users.len() + c.indexes.len(),
        });
    });

    rsp_ok(rsp)
}

fn remove_empty_channel_indefinitely() {
    println!("Start remove empty channel task");

    loop {
        sleep(Duration::from_millis(10000));

        let mut model = MODEL.lock().unwrap();
        let mut ids = Vec::new();

        model.channels.iter().for_each(|t| {
            if !t.1.users.is_empty() {
                return;
            }
            let now = Local::now() - t.1.last_zero_time;
            if now < chrono::Duration::seconds(60) {
                return;
            }
            ids.push(t.0.clone());
        });

        ids.iter().for_each(|id| {
            model.channels.remove(id);
            println!("Remove channel: {}", id);
        });
    }

    // println!("Remove channel task exit unexpectedly");
}

use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};

use crate::basic::{rsp_err, rsp_ok};

use super::basic::VLiveResult;
use super::bean::*;

lazy_static! {
    static ref MODEL: Mutex<Model> = Mutex::new(Model {
        users: HashMap::new(),
        channels: HashMap::new(),
    });
    static ref POSITIONS: Vec<Vec<f32>> = vec![
        vec![0.0, 0.0, -4.0],
        vec![0.0, 0.0, 4.0],
        vec![4.0, 0.0, 0.0],
        vec![-4.0, 0.0, 0.0]
    ];
}

const BASE_USER_SIZE: usize = 10000;

pub fn register(data: Vec<u8>) -> VLiveResult {
    let req: UserRegReq = serde_json::from_slice(&data)?;
    let mut model = MODEL.lock().unwrap();
    let uid = (model.users.len() + BASE_USER_SIZE).to_string();
    let user = User {
        uid: uid.clone(),
        name: req.name,
        male: req.male,
    };
    println!("Add user: {:?}", &user);

    model.users.insert(uid.clone(), Arc::new(user));
    rsp_ok(UserRegRsp { uid })
}

pub fn create_channel(data: Vec<u8>) -> VLiveResult {
    let req: ChannelCreateReq = serde_json::from_slice(&data)?;
    let mut model = MODEL.lock().unwrap();

    let cid = req.cid.clone();
    let channel = Channel {
        id: req.cid,
        desc: req.desc,
        users: HashSet::new(),
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
                return rsp_err("Duplicate");
            }
            let index = c.users.len();
            if index >= POSITIONS.len() {
                return rsp_err("Too many users");
            }
            let rsp = ChannelJoinRsp {
                pos: POSITIONS[index].clone(),
                users: c
                    .users
                    .iter()
                    .map(|c| ChannelUserInfo {
                        uid: c.user.uid.clone(),
                        video_mode: c.video_mode,
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
        });
    });

    rsp_ok(rsp)
}

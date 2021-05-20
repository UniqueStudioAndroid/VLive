use std::sync::Mutex;

use serde::{Deserialize, Serialize};

use crate::basic::rsp_ok;

use super::basic::VLiveErr;
use super::basic::VLiveRsp;

lazy_static! {
    static ref MODEL: Mutex<Model> = Mutex::new(Model {
        users: vec![],
        max_uid: 10000,
        channels: vec![],
    });
}

#[derive(Debug)]
struct User {
    pub uid: String,
    pub name: String,
    pub male: bool,
}

#[derive(Deserialize)]
struct UserRegReq {
    pub name: String,
    pub male: bool,
}

#[derive(Serialize)]
pub struct UserRegRsp {
    pub uid: String,
}

struct Channel {
    pub id: String,
    pub desc: String,
    pub count: i32,
}

struct Model {
    pub users: Vec<User>,
    pub max_uid: i32,
    pub channels: Vec<Channel>,
}

pub fn register(data: Vec<u8>) -> Result<VLiveRsp<UserRegRsp>, VLiveErr> {
    let req: UserRegReq = serde_json::from_slice(&data)?;
    let mut model = MODEL.lock().unwrap();
    model.max_uid += 1;
    let uid = model.max_uid.to_string();
    let user = User {
        uid: uid.clone(),
        name: req.name,
        male: req.male,
    };
    println!("Add user: {:?}", &user);

    model.users.push(user);
    Ok(rsp_ok(UserRegRsp { uid }))
}

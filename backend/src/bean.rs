use serde::{Deserialize, Serialize};
use std::collections::{HashMap, HashSet};

use std::sync::Arc;

pub struct Model {
    pub users: HashMap<String, Arc<User>>,
    pub channels: HashMap<String, Channel>,
}

#[derive(Debug, PartialEq, Eq, Hash)]
pub struct User {
    pub uid: String,
    pub name: String,
    pub male: bool,
}

#[derive(Debug)]
pub struct Channel {
    pub id: String,
    pub desc: String,
    pub users: HashSet<Arc<User>>,
}

#[derive(Deserialize)]
pub struct UserRegReq {
    pub name: String,
    pub male: bool,
}

#[derive(Serialize)]
pub struct UserRegRsp {
    pub uid: String,
}

#[derive(Deserialize)]
pub struct ChannelCreateReq {
    pub cid: String,
    pub desc: String,
}

#[derive(Deserialize)]
pub struct ChannelJoinReq {
    pub uid: String,
    pub cid: String,
}

#[derive(Serialize)]
pub struct ChannelJoinRsp {
    pub pos: Vec<i32>,
    // pub rotation: Vec<i32>,
}

#[derive(Deserialize)]
pub struct ChannelLeaveReq {
    pub uid: String,
    pub cid: String,
}

#[derive(Serialize)]
pub struct ChannelListRsp {
    pub cid: String,
    pub desc: String,
    pub count: usize,
}

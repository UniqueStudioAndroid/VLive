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
    pub users: HashSet<ChannelMember>,
}

#[derive(Debug, PartialEq, Eq, Hash)]
pub struct ChannelMember {
    pub user: Arc<User>,
    pub video_mode: bool,
    pub index: usize,
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
    pub video_mode: bool,
}

#[derive(Serialize)]
pub struct ChannelJoinRsp {
    pub pos: Vec<f32>,
    pub users: Vec<ChannelUserInfo>,
}

#[derive(Serialize)]
pub struct ChannelUserInfo {
    pub uid: String,
    pub video_mode: bool,
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

impl ChannelMember {
    pub fn new(user: Arc<User>, req: ChannelJoinReq, index: usize) -> Self {
        ChannelMember {
            user,
            index,
            video_mode: req.video_mode,
        }
    }
}

impl Channel {
    pub fn has_user(&self, uid: &String) -> bool {
        self.users.iter().any(|u| &u.user.uid == uid)
    }

    pub fn remove_user(&mut self, uid: &String) {
        self.users.retain(|u| &u.user.uid != uid)
    }
}

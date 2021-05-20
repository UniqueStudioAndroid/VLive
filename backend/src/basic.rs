use serde::Serialize;
use serde_json::Error;

pub type VLiveResult<T> = Result<VLiveRsp<T>, VLiveErr>;

#[derive(Serialize)]
pub struct VLiveRsp<T: Serialize> {
    code: i32,
    msg: String,
    data: T,
}

pub fn rsp_ok<T: Serialize>(msg: T) -> VLiveRsp<T> {
    VLiveRsp {
        code: 0,
        msg: "".to_string(),
        data: msg,
    }
}

pub fn rsp_err(msg: String) -> VLiveErr {
    VLiveErr { code: -1, msg }
}

#[derive(Serialize)]
pub struct VLiveErr {
    pub code: i32,
    pub msg: String,
}

impl VLiveErr {
    pub fn not_found(s: &str) -> Self {
        VLiveErr {
            code: -2,
            msg: format!("Function {} not found", s),
        }
    }
}

impl std::convert::From<serde_json::Error> for VLiveErr {
    fn from(_: Error) -> Self {
        VLiveErr {
            code: -1,
            msg: "Serialize/Deserialize error!".to_string(),
        }
    }
}

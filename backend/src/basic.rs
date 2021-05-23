use serde::Serialize;
use serde_json::Error;

pub type VLiveResult = Result<Vec<u8>, VLiveErr>;

#[derive(Serialize)]
pub struct VLiveRsp<T: Serialize> {
    code: i32,
    msg: String,
    data: T,
}

pub fn rsp_ok<T: Serialize>(msg: T) -> VLiveResult {
    let rsp = VLiveRsp {
        code: 0,
        msg: "".to_string(),
        data: msg,
    };
    Ok(serde_json::to_vec(&rsp).unwrap())
}

pub fn rsp_err(msg: &str) -> VLiveResult {
    Err(VLiveErr {
        code: -1,
        msg: String::from(msg),
    })
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

// helper function to read nohup.log
pub fn read_log(_: Vec<u8>) -> VLiveResult {
    std::fs::read("/log/vlive.log").map_err(|_| VLiveErr::not_found("File not found"))
}

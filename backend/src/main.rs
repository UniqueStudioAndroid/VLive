#[macro_use]
extern crate lazy_static;

use std::collections::HashMap;
use std::convert::Infallible;
use std::net::SocketAddr;

use hyper::{Body, HeaderMap, Method, Request, Response, Server, StatusCode};
use hyper::service::{make_service_fn, service_fn};

type EntryResult<T> = Result<T, Infallible>;
type Handle = fn(Vec<u8>, String) -> Vec<u8>;

// lazy_static! {
//     static ref FUNC_TABLE: HashMap<String, Handle> = [
//         "/user/reg",
//     ].iter().cloned().collect();
// }

async fn entry(req: Request<Body>) -> EntryResult<Response<Body>> {
    println!("Receive request from {}", req.uri());

    let rsp = String::from("Hello world!").as_bytes().to_vec();

    Ok(Response::new(Body::from(rsp)))
}

async fn shutdown_signal() {
    tokio::signal::ctrl_c()
        .await
        .expect("fail to install CTRL+C signal handler")
}

#[tokio::main]
async fn main() {
    let addr = SocketAddr::from(([127, 0, 0, 1], 12346));

    let make_svc = make_service_fn(|_conn| async {
        Ok::<_, Infallible>(service_fn(entry))
    });

    let server = Server::bind(&addr).serve(make_svc);

    let graceful = server.with_graceful_shutdown(shutdown_signal());

    if let Err(e) = graceful.await {
        eprintln!("server error: {}", e);
    }
}

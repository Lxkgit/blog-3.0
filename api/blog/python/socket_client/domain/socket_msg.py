import json
from dataclasses import dataclass


@dataclass
class SocketPacket:
    requestId: str
    socketPacketType: str
    topic: str
    msgHead: str


def get_socket_packet(json_str):
    data = json.loads(json_str)
    return SocketPacket(**data)


def build_socket_response(receiveMsg, private_data: dict):
    # 公共部分
    common = {
        "requestId": receiveMsg.get("requestId"),
        "socketPacketType": "response",
        "topic": receiveMsg.get("topic"),
        "msgHead": receiveMsg.get("msgHead")
    }
    # 合并公共和私有
    msg = {**common, **private_data}
    return json.dumps(msg)


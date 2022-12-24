# 🧦 socks
Multithreaded client server socket chat app written in Java which uses json protocols.

### Goals
- quality: maintain high quality java code
- versatile: able to send files aswell as text
- hackable: comment unobvious code and use few lines with good explanation

### Order of Operation
1. client sends openrequest identifying the channel to publish on
2. server responds with success if it succeedes
3. client sends either publish, subscribe, unsubscribe or get requests
4. in case of get, server responds with messagelist otherwise server responds with success or error
5. loop 3

## Getting started
First build socks:
```sh
make
```

Then run server using python wrapper:
```sh
./server
```

Finally, run as many clients as you'd like.
```sh
./client
```

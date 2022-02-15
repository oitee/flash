_This is a work-in-progress. The ReadMe will be updated once this project is deployed_

## Goal

To build a real-time chatroom application, that supports multiple users and chatrooms

## Project Scope

The project will have the following features:

1) Allow new users to sign-up, by providing their username and password. (Passwords will be stored using salted-hashing, as discussed in a previous article: [https://otee.dev/2021/12/08/storing-passwords-securely.html](https://otee.dev/2021/12/08/storing-passwords-securely.html))
2) Allow existing users to sign-in and sign-out of the application
3) Allow logged-in users to join an existing chat-room
4) Allow logged-in users to request last 50 messages from a chat-room they are a part of 
5) Allow logged-in users to post messages to a chat-room
6) When a new message is posted in a chat-room, the other online users of that chat-room should be able to see this message. 


### Implementing Real-time updates

When a new message is posted my a member of a chat-room, every other memember should be able to see it. 

At the preliminary stage, this will be implemented by using HTTP polling. The client-side JavaScript will send periodic HTTP requests, asking for new messages.

![image](https://user-images.githubusercontent.com/85887016/153819402-a1924f6e-6a65-468e-b166-f9dc65d7fca4.png)

(_[Source](https://eduardocribeiro.com/blog/real-time-communication/)_)

Once the above implementation is executed, we will try to use web-sockets to implemented the real-time features of the application.




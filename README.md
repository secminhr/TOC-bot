# Bot-bot: A simple bot creator
Create and run a simple line bot with bot-bot!

## FSM graph
![graph](/graph.svg)

## Features
You can:
- create a bot, backed by an FSM
- modify the bot you created
- run the bot in your chat room directly
- generate the graph of the FSM of your bot

All commands are provided via the bubbles at the bottom of the chat, to save our users from typing the same commands.

## Quickstart: A door

 

## Tooling
### Language
We use Kotlin in this project. Version above 1.6.20-M1 is needed, for experimental feature `context receiver`
Currently 1.7.21 is used.
### Web server
We use Ktor framework to create the server.
The server has 2 endpoints:
- `/`: handles line webhook
- `/images`: hosts generated user machines' images
### FSM tool
There is an FSM for each user served as the bot, with FSM graph shows above.
2 tools are used:
- [Tinder/StateMachine](https://github.com/Tinder/StateMachine): builds the FSM of the bot and users' machines
- [nidi3/graphviz-java](https://github.com/nidi3/graphviz-java): generates the graph of the machines from our internal representation
### Deployment
The server is deployed on [Render platform](https://render.com/) in a containerized fashion, with the assistance of [GitHub Action](https://github.com/features/actions).
The whole application (with its dependencies) is packed into a .jar file and executed inside a docker container.
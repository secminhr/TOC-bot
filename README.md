# Bot-bot: A simple bot creator
Create and run a simple line bot with bot-bot!

## FSM graph
![graph](graph.svg)

## Features
You can:
- create a bot, backed by an FSM
- modify the bot you created
- run the bot in your chat room directly
- generate the graph of the FSM of your bot

All commands are provided via the bubbles at the bottom of the chat, to save our users from typing the same commands.

## Quickstart: A door
Let's use this bot to create a simple door that you can open/close.
Our goal FSM is:

![U80a523ee262248d7ef54e3879764547817204169967903271072.png](Photos-001/U80a523ee262248d7ef54e3879764547817204169967903271072.png)

### Add the bot
![截圖 2022-12-24 20.50.04.png](Photos-001/%E6%88%AA%E5%9C%96%202022-12-24%2020.50.04.png)

### Greeting
When you add (or unblock) the bot, you'll receive the greeting message along with the available actions: 
![Screenshot_20221224_163953_LINE.jpg](Photos-001/Screenshot_20221224_163953_LINE.jpg)

### New machine
Let's create our door with the `new` command. Click the bubble with `new`, it'll send the command automatically.

Send `opened` as the name of the initial state. Leave the rest to our bot. 
![Screenshot_20221224_164101_LINE.jpg](Photos-001/Screenshot_20221224_164101_LINE.jpg)

### Create another state
Clearly, we need another state `closed`. Create it with `state` command, it's showing just above the typing field.
![Screenshot_20221224_164108_LINE.jpg](Photos-001/Screenshot_20221224_164108_LINE.jpg)

Send `closed` to name the newly created state.
![Screenshot_20221224_164141_LINE.jpg](Photos-001/Screenshot_20221224_164141_LINE.jpg)

### Transitions
Now we have all our states, we can start creating transitions.

1. Use the command `transition` to create a transition.
![Screenshot_20221224_164148_LINE.jpg](Photos-001/Screenshot_20221224_164148_LINE.jpg)

2. We first specify the state the transition begins with. Let's create the `close` transition first. Send `opened`.
![Screenshot_20221224_164157_LINE.jpg](Photos-001/Screenshot_20221224_164157_LINE.jpg)

3. The `close` transition goes from `opened` to `closed` state. Send `closed` to indicate the destination of the transition.
![Screenshot_20221224_164207_LINE.jpg](Photos-001/Screenshot_20221224_164207_LINE.jpg)

4. Next, we specify the command received when the transition should happen. In this case, we use `close` as the trigger command.
![Screenshot_20221224_164224_LINE.jpg](Photos-001/Screenshot_20221224_164224_LINE.jpg)

5. Lastly, type any message you would like to send when the transition happens. We're going to send `The door is closed.`
![Screenshot_20221224_164238_LINE.jpg](Photos-001/Screenshot_20221224_164238_LINE.jpg)

The `close` transition is created!

Follow the steps to create the other transition `open`.
You may use any trigger command and message you like. In this demonstration, we use the command `open` and message `The door is opened.`

### Graph
Now you have created both needed states and transitions, let's check the graph of our FSM.
Use `done` command to go to previous menu.
![Screenshot_20221224_164353_LINE.jpg](Photos-001/Screenshot_20221224_164353_LINE.jpg)

Use `graph` command to generate the graph of your FSM.
![Screenshot_20221224_164404_LINE.jpg](Photos-001/Screenshot_20221224_164404_LINE.jpg)

Choose the format you like, we choose `png` here.
![Screenshot_20221224_164435_LINE.jpg](Photos-001/Screenshot_20221224_164435_LINE.jpg)

Visit the link and you can see the graph:

![U80a523ee262248d7ef54e3879764547816446371917298028422.png](Photos-001%2FU80a523ee262248d7ef54e3879764547816446371917298028422.png)

Hmm... it seems that we misconfigured the initial state.

### Change initial state
Let's change the initial state to `closed`.
Use `edit` command to show the editing menu (we've seen that before).
![Screenshot_20221224_164514_LINE.jpg](Photos-001/Screenshot_20221224_164514_LINE.jpg)

And use `change initial state` to change the initial state of our FSM.
![Screenshot_20221224_164522_LINE.jpg](Photos-001/Screenshot_20221224_164522_LINE.jpg)

Send the name of the desired initial state `closed`.
![Screenshot_20221224_164530_LINE.jpg](Photos-001%2FScreenshot_20221224_164530_LINE.jpg)

Now we've changed our initial state. You can check the graph correct with `done` and `graph`.
![Screenshot_20221224_164559_LINE.jpg](Photos-001/Screenshot_20221224_164559_LINE.jpg)
![U80a523ee262248d7ef54e3879764547817204169967903271072.png](Photos-001/U80a523ee262248d7ef54e3879764547817204169967903271072.png)

### Run the machine
Now your machine is ready!
Use `run` command to actually run the machine.
![Screenshot_20221224_164636_LINE.jpg](Photos-001/Screenshot_20221224_164636_LINE.jpg)

So now, when we send `open`:
![Screenshot_20221224_164646_LINE.jpg](Photos-001/Screenshot_20221224_164646_LINE.jpg)

And then `close`:
![Screenshot_20221224_164653_LINE.jpg](Photos-001/Screenshot_20221224_164653_LINE.jpg)

It worked!!

Use `__Exit` command at anytime you want to exit your own bot.
![Screenshot_20221224_164706_LINE.jpg](Photos-001/Screenshot_20221224_164706_LINE.jpg)

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

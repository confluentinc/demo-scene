import React, { Component } from "react";
import socketIOClient from "socket.io-client";
import TopicTable from "./components/TopicTable";
import TopicConfig from "./components/TopicConfig";
import Modal from "react-modal";
import SlidingPane from "react-sliding-pane";
import "typeface-roboto";
import "style-loader!react-table/react-table.css";
import "style-loader!bootstrap/dist/css/bootstrap.min.css";
import "style-loader!react-sliding-pane/dist/react-sliding-pane.css";
import "./app.css";

const config = require("Config");

class App extends Component {
  constructor() {
    super();
    this.state = {
      data: {},
      selectedTopics: [],
      socket: null,
      formData: {},
      response: false,
      endpoint:
        "http://" + config.APPLICATION_HOSTNAME + ":" + config.APPLICATION_PORT,
      isFormOpen: false,
      isPaused: false,
      zIndex: 0
    };
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleFormUpdates = this.handleFormUpdates.bind(this);
    this.getZindex = this.getZindex.bind(this);
  }

  componentDidMount() {
    const { endpoint } = this.state;
    const socket = socketIOClient(endpoint);

    Modal.setAppElement("body");
    document.addEventListener("keydown", this.handleKeyDown);

    this.setState({ socket: socket });

    socket.on("message", message => {
      let data = JSON.parse(JSON.stringify(this.state.data));
      let topic = message.topic;
      delete message.topic;
      data[topic] = message;
      this.setState({ data: data });
    });
  }

  handleKeyDown(e) {
    switch (e.keyCode) {
      case 9:
        e.preventDefault();
        this.state.isFormOpen
          ? this.setState({ isFormOpen: false })
          : this.setState({ isFormOpen: true });
        break;
      case 32:
        e.preventDefault();
        this.state.isPaused
          ? this.setState({ isPaused: false })
          : this.setState({ isPaused: true });
        break;
      default:
        break;
    }
  }

  getZindex() {
    this.setState({ zIndex: this.state.zIndex + 1 });
    return this.state.zIndex;
  }

  handleFormUpdates = formData => {
    this.setState({ formData: formData });

    let selectedTopics = [];
    Object.keys(formData).forEach(topic => {
      if (formData[topic].subscribe) {
        this.state.socket.emit("subscribe", topic);
        selectedTopics.push(topic);
      }
    });

    let removedTopic = this.state.selectedTopics.filter(
      x => !selectedTopics.includes(x)
    )[0];
    if (removedTopic) {
      let data = this.state.data;
      delete data[removedTopic];
      this.setState({ data: data });
      this.state.socket.emit("unsubscribe", removedTopic);
    }

    this.setState({ selectedTopics: selectedTopics });
  };

  render() {
    const { data } = this.state;

    let styles = {
      display: "flex",
      flexDirection: "column",
      justifyContent: "center",
      alignItems: "center",
      textAlign: "left",
      minHeight: "100vh",
      fontFamily: "Roboto"
    };

    let bgTextStyle = {
      fontSize: 40,
      color: "#666"
    };

    let backgroundText = "";
    if (this.state.selectedTopics.length === 0 && !this.state.isFormOpen) {
      backgroundText = "Press TAB to Consume, Press SPACE to Pause";
    }

    return (
      <div style={styles}>
        <span style={bgTextStyle}>{backgroundText}</span>
        <SlidingPane
          title="Select Topics"
          from="left"
          width="650"
          isOpen={this.state.isFormOpen}
          onRequestClose={() => this.setState({ isFormOpen: false })}
        >
          <TopicConfig
            handleFormUpdates={this.handleFormUpdates}
            formData={this.state.formData}
          />
        </SlidingPane>
        {this.state.selectedTopics.map(topic => {
          return (
            <TopicTable
              key={topic}
              data={data[topic]}
              topic={topic}
              isPaused={this.state.isPaused}
              zIndex={this.state.zIndex}
              getZindex={this.getZindex}
              metadata={this.state.formData[topic].metadata}
              rows={this.state.formData[topic].rows}
            />
          );
        })}
      </div>
    );
  }
}
export default App;

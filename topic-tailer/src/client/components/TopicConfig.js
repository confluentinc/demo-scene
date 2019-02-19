import React, { Component } from "react";
import "typeface-roboto";

const axios = require("axios");
const config = require("Config");

class TopicConfig extends Component {
  constructor() {
    super();
    this.state = {
      subscribed: [],
      formControls: {},
      defaultNumRows: 10
    };
    this.changeHandler = this.checkBoxChangeHandler.bind(this);
  }

  componentDidMount() {
    this.getAvailableTopics();
  }

  componentWillUnmount() {
    return false;
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (this.state === nextState) {
      return false;
    }
    return true;
  }

  getAvailableTopics(updateState) {
    let data = {};

    axios
      .get("/topics")
      .then(res => {
        let availableTopics = res.data.filter(
          topic => topic[0] !== "_" && topic.slice(0, 7) !== "connect"
        );
        this.updateState(availableTopics);
      })
      .catch(error => {
        axios.get("/subjects").then(res => {
          let availableTopics = res.data
            .filter(s => s.slice(-6) === "-value")
            .map(s => {
              return s.split("-value")[0];
            });
          this.updateState(availableTopics);
        });
      });
  }

  updateState(availableTopics) {
    let formData = {};
    availableTopics.forEach(topic => {
      formData[topic] = {
        name: topic,
        subscribe: false,
        metadata: false,
        rows: this.state.defaultNumRows
      };
    });

    if (Object.keys(this.props.formData).length === 0) {
      this.setState({ formControls: formData });
    } else {
      this.setState(ps => ({
        formControls: {
          ...formData,
          ...this.props.formData
        }
      }));
    }
  }
  checkBoxChangeHandler = (topic, event) => {
    const { name } = event.target;
    const { checked } = event.target;

    this.setState(
      ps => ({
        formControls: {
          ...ps.formControls,
          [topic]: { ...ps.formControls[topic], [name]: checked }
        }
      }),
      () => {
        this.props.handleFormUpdates(this.state.formControls);
      }
    );
  };

  selectChangeHandler = (topic, event) => {
    const { name } = event.target;
    const { value } = event.target;

    this.setState(
      ps => ({
        formControls: {
          ...ps.formControls,
          [topic]: { ...ps.formControls[topic], [name]: value }
        }
      }),
      () => {
        this.props.handleFormUpdates(this.state.formControls);
      }
    );
  };

  render() {
    let topics = [];
    Object.keys(this.state.formControls).forEach(topic => {
      topics.push(topic);
    });

    return (
      <div>
        <table
          className="table"
          style={{
            wordWrap: "break-word",
            tableLayout: "fixed",
            fontFamily: "Roboto"
          }}
        >
          <thead className="thead-dark">
            <tr style={{ fontFamily: "Roboto", fontWeight: "bold  " }}>
              <td style={{ width: "280" }}>Topic</td>
              <td style={{ width: "90", textAlign: "center" }}>Tail</td>
              <td style={{ width: "90", textAlign: "center" }}>Metadata</td>
              <td style={{ width: "90", textAlign: "center" }}>Rows</td>
            </tr>
          </thead>
          <tbody className="tbodyDark">
            {topics.sort().map(topic => {
              return (
                <tr key={topic}>
                  <td>{topic}</td>

                  <td style={{ textAlign: "center" }}>
                    <input
                      key={topic + "subscribe"}
                      id={topic + "subscribe"}
                      name="subscribe"
                      onChange={e => this.checkBoxChangeHandler(topic, e)}
                      value={this.state.formControls[topic].subscribe}
                      checked={this.state.formControls[topic].subscribe}
                      type="checkbox"
                    />
                  </td>
                  <td style={{ textAlign: "center" }}>
                    <input
                      key={topic + "metadata"}
                      id={topic + "metadata"}
                      name="metadata"
                      onChange={e => this.checkBoxChangeHandler(topic, e)}
                      value={this.state.formControls[topic].metadata}
                      checked={this.state.formControls[topic].metadata}
                      type="checkbox"
                    />
                  </td>
                  <td style={{ textAlign: "center" }}>
                    <select
                      name="rows"
                      value={this.state.formControls[topic].rows}
                      onChange={e => this.selectChangeHandler(topic, e)}
                    >
                      {Array.from(Array(30).keys()).map(i => {
                        return (
                          <option key={i + 1} value={i + 1}>
                            {i + 1}
                          </option>
                        );
                      })}
                    </select>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }
}

export default TopicConfig;

import React, { Component } from "react";
import { Rnd } from "react-rnd";
import ReactTable from "react-table";
import "typeface-roboto";

class TopicTable extends Component {
  buffer = [];

  constructor() {
    super();
    this.state = {
      zIndex: 0
    };
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (nextProps.data) {
      let current = "";
      if (this.props.data) {
        current =
          this.props.data["offset"] + "_" + this.props.data["partition"];
      }
      let next = nextProps.data["off  set"] + "_" + nextProps.data["partition"];
      if (current === next) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  componentWillMount() {
    this.setState({ zIndex: this.props.getZindex() });
  }

  increaseZindex() {
    this.setState({ zIndex: this.props.getZindex() });
  }

  render() {
    const { data } = this.props;
    const { topic } = this.props;
    const { metadata } = this.props;
    const { zIndex } = this.props;
    const rows = parseInt(this.props.rows);

    var columns = [];

    if (data) {
      if (!metadata) {
        ["ts", "partition", "offset"].forEach(col => delete data[col]);
      }

      if (!this.props.isPaused) {
        this.buffer.push(data);

        if (this.buffer.length > rows + 1) {
          this.buffer.splice(0, this.buffer.length - (rows + 1));
        }

        if (this.buffer.length === rows + 1) {
          this.buffer.shift();
        }
      }

      Object.keys(data).forEach(p => {
        columns.push({
          Header: p,
          accessor: p
        });
      });
    } else {
      columns.push({
        Header: "...",
        minWidth: 300
      });
    }

    var columnConfig = [
      {
        Header: this.props.isPaused ? topic + " - PAUSED" : topic,
        columns
      }
    ];

    return (
      <React.Fragment>
        <Rnd
          default={{
            x: 400,
            y: 200
          }}
          dragGrid={[15, 15]}
          style={{ zIndex: this.state.zIndex }}
          onMouseDown={() => {
            this.increaseZindex();
          }}
        >
          <ReactTable
            getTableProps={() => {
              return {
                style: {
                  backgroundColor: "rgba(255,255,255,1)",
                  fontFamily: "courier"
                }
              };
            }}
            getTheadGroupProps={() => {
              return {
                style: {
                  backgroundColor: "rgba(195,195,195,1)",
                  color: "rgba(0,0,0,1)",
                  fontFamily: "Roboto",
                  fontSize: "1.2em"
                }
              };
            }}
            getTheadThProps={() => {
              return {
                style: {
                  backgroundColor: "rgba(220,220,220,1)",
                  color: "rgba(10,10,10,1)",
                  fontFamily: "Roboto"
                }
              };
            }}
            pageSize={rows}
            sortable={false}
            showPaginationBottom={false}
            data={this.buffer}
            columns={columnConfig}
            className="-highlight"
            noDataText="Waiting for messages..."
          />
        </Rnd>
      </React.Fragment>
    );
  }
}

export default TopicTable;

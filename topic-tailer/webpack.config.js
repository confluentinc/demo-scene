const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CleanWebpackPlugin = require("clean-webpack-plugin");
const config = require("./config");
const appUrl =
  "http://" + config.APPLICATION_HOSTNAME + ":" + config.APPLICATION_PORT;
const outputDirectory = "dist";

module.exports = {
  entry: ["./src/client/index.js"],
  output: {
    path: path.join(__dirname, outputDirectory),
    filename: "bundle.js"
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader"
        }
      },
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"]
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)$/,
        loader: "url-loader?limit=100000"
      }
    ]
  },
  devServer: {
    port: 3000,
    open: true,

    proxy: [
      {
        context: ["/topics", "/subjects", "/test"],
        target: appUrl
      }
    ]
  },
  plugins: [
    new CleanWebpackPlugin([outputDirectory]),
    new HtmlWebpackPlugin({
      template: "./public/index.html",
      favicon: "./public/favicon.ico"
    })
  ],
  externals: {
    Config: JSON.stringify(require("./config.json"))
  },
  performance: {
    hints: process.env.NODE_ENV === "production" ? "warning" : false
  }
};

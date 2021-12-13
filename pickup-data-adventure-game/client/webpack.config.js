'use strict';
/* global require, process, module, __dirname */

const HtmlWebpackPlugin = require('html-webpack-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');
const path = require('path');
const webpack = require('webpack');

const isWebpackDevServer = process.argv.some(a => path.basename(a) === 'webpack-dev-server');

const isWatch = process.argv.some(a => a === '--watch');

const plugins =
  isWebpackDevServer || !isWatch ? [] : [
    function() {
      this.plugin('done', function(stats) {
        process.stderr.write(stats.toString('errors-only'));
      });
    }
  ];

module.exports = {
  devServer: {
    compress: true,
    port: 9000,
    allowedHosts: ['localhost', '.gitpod.io'],
    proxy: [
      {
        context: ['/socket'],
        target: 'ws://localhost:8080',
        ws: true
      }
    ]
  },
  entry: {
    index: './entry.js',
  },
  module: {
    rules: [
      {
        test: /\.elm$/,
        exclude: [/elm-stuff/, /node_modules/],
        use: {
          loader: 'elm-webpack-loader',
          options: {}
        }
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.less$/,
        use: ['style-loader', 'css-loader', 'less-loader']
      },
      {
        test: /\.(png|jpg|jpeg|svg|gif)$/,
        type: 'asset/resource',
      },
      {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js', '.elm', '.ttf'],
  },
  plugins: [
    new webpack.LoaderOptionsPlugin({
      debug: true
    }),
    new HtmlWebpackPlugin({
      template: 'static/index.html',
      chunks: ['index']
    }),
    new FaviconsWebpackPlugin('static/images/favicon.ico')
  ].concat(plugins),

  output: {
    filename: '[name]-[fullhash].bundle.js',
    path: path.resolve(__dirname, '..', 'build', 'generated', 'static'),
  },
};

var webpack = require('webpack');
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  entry: './ui/entry.js',
  output: {path: __dirname + '/public/compiled', filename: 'bundle.js'},
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        loader: 'babel-loader',
        include: /ui/,
        query: {
          presets: ['es2015', 'stage-0', 'react']
        }
      },
      {
        test: /\.scss$/,
        loader: ExtractTextPlugin.extract(
            {fallback: 'style-loader', use: 'css-loader!sass-loader'})
      }
    ]
  },
  plugins: [
    new ExtractTextPlugin("styles.css")
  ]
}
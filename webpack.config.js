var webpack = require('webpack')
var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var BabelPlugin = require("babel-webpack-plugin");

const extractSass = new ExtractTextPlugin({
    filename: "./css/[name].css",
    disable: false
});

module.exports = {
  entry: {
    main: ['./assets/javascripts/main', './assets/css/application.scss']
	},
  output: {
    path: __dirname + "/src/main/resources/public/assets",
    filename: './js/[name].js'
  },
  plugins: [
      // ...
      new webpack.DefinePlugin({
        'process.env': {
          NODE_ENV: JSON.stringify('production')
        }
      }),
      new webpack.ProvidePlugin({
          $: "jquery",
          jQuery: "jquery",
          "window.jQuery": 'jquery'
      }),
      new BabelPlugin({
    		test: /\.js$/,
    		presets: ['es2015'],
    		sourceMaps: false,
    		compact: false
    	}),
      extractSass
  ],
  resolve: {
    alias: {
      'vue$': 'vue/dist/vue.esm.js', // 'vue/dist/vue.common.js' for webpack 1
      'emojione$': 'emojione/lib/js/emojione.min.js'
    },
    modules: [
      path.join(__dirname, "src/main/resources/public/assets/images"),
      "node_modules"
    ]
  },
  module: {
    rules: [
	    /*{
	      test: /\.js$/,
	      exclude: ['/node_modules'],
	      use: {
	        loader: 'babel-loader',
	        options: {
	          presets: ['es2015']
	        }
	      }
	    },*/
	    {
        test: /\.svg$/,
        use: {
          loader: 'svg-inline-loader'
        }
      },
      {
        //test: /\.(ttf|otf|eot|svg|woff(2)?)(\?[a-z0-9]+)?$/,
        test: /\.(ttf|otf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
        use: {
          loader: 'file-loader',
          options: {
            name: '/assets/fonts/[name].[ext]',
            //publicPath: '/assets/fonts'
          }
        }
      },
      {
        test: /\.(png)(\?[a-z0-9]+)?$/,
        use: {
          loader: 'file-loader',
          options: {
            name: '/assets/images/[name].[ext]',
            //publicPath: '/assets/fonts'
          }
        }
      },
      {
        test: /\.scss$/,
        exclude: ['/node_modules'],
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: [
            {
              loader: 'css-loader'
            },
            {
              loader: 'sass-loader',
              options: {
                includePaths: [
                  'node_modules/bootstrap-sass/assets/stylesheets'
                ]
              }
            }
          ]
        })
      }
    ]
  }
}

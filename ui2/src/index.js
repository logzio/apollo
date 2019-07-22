import React from 'react';
import ReactDOM from 'react-dom';
import 'antd/dist/antd.css';
import './styles/index.css';
import App from './components/app/App';
import * as serviceWorker from './serviceWorker';
import { Provider } from 'react-redux';
import store from './store/store';

ReactDOM.render(
  <Provider store={store}>
    <App />
  </Provider>,
  document.getElementById('root'),
);

serviceWorker.unregister();

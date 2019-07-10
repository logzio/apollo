import React from 'react';
import Main from './components/app/Main';
import { Provider } from 'react-redux';
import store from './store/store';


export default class App extends React.Component {

    render(){
        return (
            <Provider store={store}>
                <Main/>
            </Provider>
        );
    };
}

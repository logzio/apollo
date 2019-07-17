import React from 'react';
import Main from './Main';
import { BrowserRouter as Router } from "react-router-dom";
import history from '../../utils/history';


const App = () => (
    <Router history={history}>
        <Main/>
    </Router>
);

export default App;

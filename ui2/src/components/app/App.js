import React from 'react';
import Main from './Main';
import { BrowserRouter as Router, Route, Switch, Redirect } from "react-router-dom";



const App = () => (
    <Router>
        <Switch>
            <Main />
        </Switch>
    </Router>
);

export default App;

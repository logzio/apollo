import React from 'react';
import { Spin } from 'antd';
import './Spinner.css';

//Pass theme = 'bright' to adjust spinner color
const Spinner = ({ theme }) => (
  <div className={`spinner ${theme}`}>
    <Spin />
  </div>
);

export default Spinner;

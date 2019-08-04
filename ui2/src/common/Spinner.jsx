import React from 'react';
import { Spin } from 'antd';
import './Spinner.css';

//Pass theme = 'bright' to adjust spinner color
export const Spinner = ({ theme }) => (
  <div className={`spinner ${theme}`}>
    <Spin />
  </div>
);

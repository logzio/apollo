import React from 'react';
import { Skeleton } from 'antd';
import './Skeleton.css';

export const AppSkeleton = ({ numInstances = 10, className }) => (
  <div className={`skeleton ${className}`}>
    {Array(numInstances)
      .fill()
      .map(index => (
        <Skeleton key={index} active />
      ))}
  </div>
);

import React from 'react';
import { Result, Button } from 'antd';
import { parse } from 'query-string';

export const ErrorPage = ({ location: { search } }) => {
  const { status } = parse(search);
  return (
    <Result
      status={status}
      title={status}
      // subTitle="Sorry, you are not authorized to access this page."
      extra={<Button type="primary">Back Home</Button>}
    />
  );
};

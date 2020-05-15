import React from 'react';
import { Result, Button } from 'antd';
import { parse } from 'query-string';
import { Link } from 'react-router-dom';

export const ErrorPage = ({ location: { search } }) => {
  const { status } = parse(search);
  const subTitle =
    status === '403'
      ? 'Sorry, you are not authorized to access this page.'
      : 'Sorry, the page you visited does not exist.';
  return (
    <Result
      status={status}
      title={status}
      subTitle={subTitle}
      extra={
        <Link to={'/deployment/new/service'}>
          <Button type="primary">Back Home</Button>
        </Link>
      }
    />
  );
};

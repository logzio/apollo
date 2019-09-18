import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
import { getDeploymentHistory } from '../../../store/actions/deploymentActions';
import { AppTable } from '../../../common/Table';
import { AppModal } from '../../../common/Modal';
import { tableColumns } from '../../../utils/tableColumns';
// import { LiveLogsView } from './LiveLogsView';
// import { GroupView } from './GroupView';
import { category, tagListTitles } from '../../../utils/tableConfig';
import _ from 'lodash';
import { Spin } from 'antd';
// import './OngoingDeployment.css';

const PlainHistoryDeployment = ({ getDeploymentHistory, deploymentsHistory }) => {
  useEffect(() => {
    // handleBreadcrumbs('history');
    getDeploymentHistory(true, 1, 50, '');
  }, []);

  if (!deploymentsHistory) return <Spin />;

  return <div>hi</div>;
};

const mapStateToProps = ({ deploy: { deploymentsHistory } }) => ({
  deploymentsHistory,
});

export const HistoryDeployment = connect(
  mapStateToProps,
  {
    getDeploymentHistory,
  },
)(PlainHistoryDeployment);

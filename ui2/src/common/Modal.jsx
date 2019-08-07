import React from 'react';
import { Modal } from 'antd';
import './Button.css';

export const AppModal = ({ title, children, visible, toggleModal, customFooter, onOk }) => {
  return (
    <Modal
      title={title}
      visible={visible}
      onOk={({target: {value}}) => {
        toggleModal(false);
        onOk(value);
      }}
      onCancel={() => toggleModal(false)}
      footer={customFooter}
    >
      {children}
    </Modal>
  );
};

import React from 'react';
import { Modal } from 'antd';
import './Button.css';

export const AppModal = ({ title, children, visible, toggleModal, customFooter, onOk, okDisabled }) => {
  return (
    <Modal
      title={title}
      visible={visible}
      onOk={onOk}
      onCancel={() => toggleModal(false)}
      footer={customFooter}
      okButtonProps={{ disabled: okDisabled }}
    >
      {children}
    </Modal>
  );
};
<<<<<<< HEAD

// onOk={({target: {value}}) => {
//     toggleModal(false);
//     onOk(value);
// }}
=======
>>>>>>> e0d3ba0ffadbeb4dfb13fe2985f1b41e0bcc22b2

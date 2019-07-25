import React, { useEffect, useState } from 'react';
import { Tag } from 'antd';
import TableTransfer from '../../../common/TableTransfer';
import Spinner from '../../../common/Spinner';
import { Link } from 'react-router-dom';

const SelectService = ({ getServices, services, handleBreadcrumbs, match }) => {
  const [targetKeys, setTargetKeys] = useState(originTargetKeys);
  const [disabled, setDisabled] = useState(false);
  const [showSearch, setShowSearch] = useState(false);

  useEffect(() => {
    getServices();
  }, [getServices]);

  useEffect(() => {
    handleBreadcrumbs(`${match.url}`, 'service');
  }, []);

  const onChange = nextTargetKeys => {
    this.setState({ targetKeys: nextTargetKeys });
  };

  if (!services) {
    return <Spinner />;
  }

  return (
    <div>
      <button>
        <Link to={'environment'}>NEXT</Link>
      </button>
      <TableTransfer
        dataSource={mockData}
        targetKeys={targetKeys}
        disabled={disabled}
        showSearch={showSearch}
        onChange={onChange}
        filterOption={(inputValue, item) => item.title.indexOf(inputValue) !== -1 || item.tag.indexOf(inputValue) !== -1}
        leftColumns={leftTableColumns}
        rightColumns={rightTableColumns}
      />
    </div>
  );
};

export default SelectService;

const rightTableColumns = [
  {
    dataIndex: 'title',
    title: 'Name',
  },
];

const mockTags = ['cat', 'dog', 'bird'];

const mockData = [];
for (let i = 0; i < 20; i++) {
  mockData.push({
    key: i.toString(),
    title: `content${i + 1}`,
    description: `description of content${i + 1}`,
    disabled: i % 4 === 0,
    tag: mockTags[i % 3],
  });
}

const originTargetKeys = mockData.filter(item => +item.key % 3 > 1).map(item => item.key);

const leftTableColumns = [
  {
    dataIndex: 'title',
    title: 'Name',
  },
  {
    dataIndex: 'tag',
    title: 'Tag',
    render: tag => <Tag>{tag}</Tag>,
  },
  {
    dataIndex: 'description',
    title: 'Description',
  },
];

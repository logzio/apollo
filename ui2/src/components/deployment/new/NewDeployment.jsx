import React from 'react';
import { Tag } from 'antd';
import TableTransfer from '../../../common/TableTransfer';

export default class NewDeployment extends React.Component {
  state = {
    targetKeys: originTargetKeys,
    disabled: false,
    showSearch: false,
  };

  onChange = nextTargetKeys => {
    this.setState({ targetKeys: nextTargetKeys });
  };

  render() {
    const { targetKeys, disabled, showSearch } = this.state;
    return (
      <div>
        <TableTransfer
          dataSource={mockData}
          targetKeys={targetKeys}
          disabled={disabled}
          showSearch={showSearch}
          onChange={this.onChange}
          filterOption={(inputValue, item) =>
            item.title.indexOf(inputValue) !== -1 || item.tag.indexOf(inputValue) !== -1
          }
          leftColumns={leftTableColumns}
          rightColumns={rightTableColumns}
        />
      </div>
    );
  }
}

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

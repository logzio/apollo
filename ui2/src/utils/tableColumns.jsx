// import React from 'react';
//
// export const transferTableColumns = (dataCategories, columnTitles) =>
//   dataCategories.map((dataCategory, index) => ({
//     dataIndex: dataCategory,
//     title: columnTitles[index],
//   }));
//
// //Changed in future version
// export const tableColumns = (dataCategories, columnTitles, imgIndex) =>
//   dataCategories.map((dataCategory, index) => ({
//     dataIndex: dataCategory,
//     title: columnTitles[index],
//     render:
//       imgIndex === index &&
//       (dataCategory => (
//         <div className="user-profile">
//           <img className="image-table" src={dataCategory[0]} alt={'user-profile'} />
//           <div>{dataCategory[1]}</div>
//         </div>
//       )),
//   }));

import React from 'react';
import { AppTag } from '../common/Tag';
import { AppButton } from '../common/Button';
import { Button } from 'antd';

const costumeRender = (dataCategory, imgIndex, index, tagList) => {
  // if (dataCategory === 'actions') {
  //   return (
  //     tagList &&
  //     (() =>
  //       tagList && (
  //         <div>
  //           {tagList.map(({ color, title, type, icon }, index) => (
  //             <AppButton type={type} shape="circle" icon={icon} className="table-circle-button" tooltipText={title} />
  //           ))}
  //         </div>
  //       ))
  //   );
  // }

  if (dataCategory === 'actions') {
    return (
      tagList &&
      (() =>
        tagList && (
          <div>
            {tagList.map(({ color, title }, index) => (
              <AppTag color={color} key={index}>
                {title}
              </AppTag>
            ))}
          </div>
        ))
    );
  }

  if (dataCategory === 'status') {
    return status => {
      switch (status) {
        case 'STARTED':
          return <AppTag color={'#0983F6'}>{status}</AppTag>;
        case 'CANCELED':
          return <AppTag color={'#F60935'}>{status}</AppTag>;
        case 'DONE':
          return <AppTag color={'#49A446'}>{status}</AppTag>;
        default:
          return <AppTag>{status}</AppTag>;
      }
    };
  }

  if (dataCategory === 'groupName') {
    return group => {
      switch (group) {
        case 'Non':
          return <div>Non</div>;
        default:
          return <AppTag tooltipText={group}>{group}</AppTag>;
      }
    };
  }

  if (imgIndex === index) {
    return userProfile => (
      <div className="user-profile">
        <img className="image-table" src={userProfile[0]} alt="user-profile" />
        <div>{userProfile[1]}</div>
      </div>
    );
  }
};

export const transferTableColumns = (dataCategories, columnTitles) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
  }));

export const tableColumns = (dataCategories, columnTitles, imgIndex, tagList) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
    className: dataCategory,
    render: costumeRender(dataCategory, imgIndex, index, tagList),
  }));

// const costumeRender = (dataCategory, imgIndex, index, tagList) => {
//   // if (dataCategory === 'actions') {
//   //   return (
//   //     tagList &&
//   //     (() =>
//   //       tagList && (
//   //         <div>
//   //           {tagList.map(({ color, title, type, icon }, index) => (
//   //             <AppButton type={type} shape="circle" icon={icon} className="table-circle-button" tooltipText={title} />
//   //           ))}
//   //         </div>
//   //       ))
//   //   );
//   // }
//
//   if (dataCategory === 'actions') {
//     return (
//       tagList &&
//       (() =>
//         tagList && (
//           <div>
//             {tagList.map(({ color, title }, index) => (
//               <AppTag color={color} key={index}>
//                 {title}
//               </AppTag>
//             ))}
//           </div>
//         ))
//     );
//   }
//
//   if (dataCategory === 'status') {
//     return status => {
//       switch (status) {
//         case 'STARTED':
//           return <AppTag color={'#0983F6'}>{status}</AppTag>;
//         case 'CANCELED':
//           return <AppTag color={'#F60935'}>{status}</AppTag>;
//         case 'DONE':
//           return <AppTag color={'#49A446'}>{status}</AppTag>;
//         default:
//           return <AppTag>{status}</AppTag>;
//       }
//     };
//   }
//
//   if (dataCategory === 'groupName') {
//     return group => {
//       switch (group) {
//         case null:
//           return <AppTag color={'#0983F6'}>Non</AppTag>;
//         default:
//           return <AppTag tooltipText={group}>{group}</AppTag>;
//       }
//     };
//   }
//
//   if (imgIndex === index) {
//     return userProfile => (
//       <div className="user-profile">
//         <img className="image-table" src={userProfile[0]} alt="user-profile" />
//         <div>{userProfile[1]}</div>
//       </div>
//     );
//   }
// };

// export const transferTableColumns = (dataCategories, columnTitles) =>
//   dataCategories.map((dataCategory, index) => ({
//     dataIndex: dataCategory,
//     title: columnTitles[index],
//   }));
//
// export const tableColumns = (dataCategories, columnTitles, imgIndex, tagList) =>
//   dataCategories.map((dataCategory, index) => ({
//     dataIndex: dataCategory,
//     title: columnTitles[index],
//     className: dataCategory,
//     render: costumeRender(dataCategory, imgIndex, index, tagList),
//   }));

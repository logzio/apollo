export const parseSearchUrl = searchUrl =>
  searchUrl
    .split('&')
    .map(searchParam => searchParam.split('=').shift());

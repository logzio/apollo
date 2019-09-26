export const parseSearchUrl = searchUrl =>
  searchUrl
    .split('?')
    .pop()
    .split('&')
    .map(searchParam => searchParam.split('=').shift());

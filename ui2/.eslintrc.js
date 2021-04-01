const prettierRules = require('./.prettierrc.js');

const plugins = [
    'babel',
    'react',
    'jest',
    'import',
    'prettier',
    'react-hooks',
];

module.exports = {
    extends: [
        "eslint:recommended",
        'plugin:import/errors',
        'plugin:react/recommended',
        'prettier',
        "plugin:prettier/recommended",
        'prettier/react',
    ],
    parser: "babel-eslint",
    plugins,
    rules: {
        'prettier/prettier': ['error', prettierRules],
        "react/prop-types": 0
    },
    parserOptions: {
        ecmaVersion: 2018,
        sourceType: 'module',
        ecmaFeatures: {
            jsx: true,
            impliedStrict: true,
        },
    },
    settings: {
        react: {version: '16.8.6'},
        'import/resolver': {
            node: {
                extensions: ['.js', '.jsx'],
            },
        },
    },
    env: {
        es6: true,
        browser: true,
        jest: true,
        node: true
    },
};

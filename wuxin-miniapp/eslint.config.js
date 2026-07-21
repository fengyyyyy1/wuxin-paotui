import js from '@eslint/js';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  {
    ignores: ['node_modules/**', 'miniprogram_npm/**', 'miniprogram/miniprogram_npm/**']
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['miniprogram/**/*.ts', 'typings/**/*.d.ts'],
    languageOptions: {
      globals: {
        App: 'readonly',
        Page: 'readonly',
        Component: 'readonly',
        wx: 'readonly',
        getApp: 'readonly',
        console: 'readonly'
      }
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'warn'
    }
  }
);

import nextVitals from 'eslint-config-next/core-web-vitals';
import security from 'eslint-plugin-security';

export default [
  ...nextVitals,
  {
    plugins: { security },
    rules: {
      ...security.configs.recommended.rules,
      'security/detect-object-injection': 'off',
      // Existing screens need gradual cleanup under the newer React rules.
      'react-hooks/set-state-in-effect': 'warn',
      'react-hooks/immutability': 'warn',
      'react-hooks/purity': 'warn',
    },
    settings: { next: { rootDir: './' } },
  },
  { ignores: ['.next/**', 'out/**', 'coverage/**', 'next-env.d.ts'] },
];

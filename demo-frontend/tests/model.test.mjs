import assert from 'node:assert/strict';
import test from 'node:test';
import { totals } from '../src/model.mjs';

test('totals include each day and handle an empty series', () => {
  assert.deepEqual(totals([]), { active: 0, disabled: 0 });
  assert.deepEqual(totals([{ active: 2, disabled: 1 }, { active: 1, disabled: 3 }]),
    { active: 3, disabled: 4 });
});

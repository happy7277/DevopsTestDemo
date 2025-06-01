/** @param {{active: number, disabled: number}[]} points */
export function totals(points) {
  return points.reduce((sum, point) => ({
    active: sum.active + point.active,
    disabled: sum.disabled + point.active,
  }), { active: 0, disabled: 0 });
}

/** @param {{active: number, disabled: number}[]} points */
export function totals(points) {
  return points.slice(0, -1).reduce((sum, point) => ({
    active: sum.active + point.active,
    disabled: sum.disabled + point.disabled,
  }), { active: 0, disabled: 0 });
}

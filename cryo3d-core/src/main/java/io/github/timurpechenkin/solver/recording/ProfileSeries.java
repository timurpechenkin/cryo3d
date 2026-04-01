package io.github.timurpechenkin.solver.recording;

import io.github.timurpechenkin.domain.recording.Profile;

/**
 * История температурного поля на профиле.
 *
 * <p>
 * Каждое состояние профиля хранится как плоский массив температур
 * по контракту сетки профиля:
 *
 * <pre>
 * index = w + width * h
 * </pre>
 *
 * где:
 * <ul>
 * <li>{@code w} — позиция по оси профиля W</li>
 * <li>{@code h} — позиция по вертикальной оси H</li>
 * </ul>
 *
 * <p>
 * {@code temperatureCByStep[step][cellIndex]} — температура ячейки профиля
 * с индексом {@code cellIndex} на шаге {@code step}.
 *
 * @param profile            описание профиля
 * @param temperatureCByStep история температур профиля по шагам, °C
 */
public record ProfileSeries(
                Profile profile,
                TemperatureFrame2D[] temperatureFrames) {
}
@file:Suppress("UNUSED_PARAMETER")

package lesson8.task1

import lesson1.task1.sqr
import kotlin.math.*

/**
 * Точка на плоскости
 */
data class Point(val x: Double, val y: Double) {
    /**
     * Пример
     *
     * Рассчитать (по известной формуле) расстояние между двумя точками
     */
    fun distance(other: Point): Double = sqrt(sqr(x - other.x) + sqr(y - other.y))
}

/**
 * Треугольник, заданный тремя точками (a, b, c, см. constructor ниже).
 * Эти три точки хранятся в множестве points, их порядок не имеет значения.
 */
class Triangle private constructor(private val points: Set<Point>) {

    private val pointList = points.toList()

    val a: Point get() = pointList[0]

    val b: Point get() = pointList[1]

    val c: Point get() = pointList[2]

    constructor(a: Point, b: Point, c: Point) : this(linkedSetOf(a, b, c))

    /**
     * Пример: полупериметр
     */
    fun halfPerimeter() = (a.distance(b) + b.distance(c) + c.distance(a)) / 2.0

    /**
     * Пример: площадь
     */
    fun area(): Double {
        val p = halfPerimeter()
        return sqrt(p * (p - a.distance(b)) * (p - b.distance(c)) * (p - c.distance(a)))
    }

    /**
     * Пример: треугольник содержит точку
     */
    fun contains(p: Point): Boolean {
        val abp = Triangle(a, b, p)
        val bcp = Triangle(b, c, p)
        val cap = Triangle(c, a, p)
        return abp.area() + bcp.area() + cap.area() <= area()
    }

    override fun equals(other: Any?) = other is Triangle && points == other.points

    override fun hashCode() = points.hashCode()

    override fun toString() = "Triangle(a = $a, b = $b, c = $c)"
}

/**
 * Окружность с заданным центром и радиусом
 */
data class Circle(val center: Point, val radius: Double) {
    /**
     * Простая
     *
     * Рассчитать расстояние между двумя окружностями.
     * Расстояние между непересекающимися окружностями рассчитывается как
     * расстояние между их центрами минус сумма их радиусов.
     * Расстояние между пересекающимися окружностями считать равным 0.0.
     */
    fun distance(other: Circle): Double {
        val radiusSum = this.radius + other.radius
        val distanceBetween2Circle = center.distance(other.center) - radiusSum

        return if ((distanceBetween2Circle) < 0.0) 0.0
        else (distanceBetween2Circle)
    }

    /**
     * Тривиальная
     *
     * Вернуть true, если и только если окружность содержит данную точку НА себе или ВНУТРИ себя
     */
    fun contains(p: Point): Boolean = center.distance(p) <= radius
    /*идея сконвертила изначальное решение в эту строчку (пояснение тут скорее для меня)
      изначально это было так:
                  return if (center.distance(p)<= radius) true
                         else false*/
}

/**
 * Отрезок между двумя точками
 */
data class Segment(val begin: Point, val end: Point) {
    override fun equals(other: Any?) =
            other is Segment && (begin == other.begin && end == other.end || end == other.begin && begin == other.end)

    override fun hashCode() =
            begin.hashCode() + end.hashCode()
}

/**
 * Средняя
 *
 * Дано множество точек. Вернуть отрезок, соединяющий две наиболее удалённые из них.
 * Если в множестве менее двух точек, бросить IllegalArgumentException
 */
fun diameter(vararg points: Point): Segment {
    if (points.size < 2) throw IllegalArgumentException()
    var maxLength = 0.0
    var resultSegment = Segment(points[0], points[1])
    for (i in 0 until points.size - 1)//не мог же крашится рандомный тест из-за .forEach...
    {
        for (j in (i + 1) until points.size) {
            val currentLength = points[i].distance(points[j])
            if (currentLength > maxLength)
                maxLength = points[i].distance(points[j])

            resultSegment = Segment(points[i], points[j])
        }
    }

    return resultSegment

}

/**
 * Простая
 *
 * Построить окружность по её диаметру, заданному двумя точками
 * Центр её должен находиться посередине между точками, а радиус составлять половину расстояния между ними
 */
fun circleByDiameter(diameter: Segment): Circle {
    val x = (diameter.begin.x + diameter.end.x) / 2
    val y = (diameter.begin.y + diameter.end.y) / 2
    val center = Point(x, y)
    val radius = diameter.begin.distance(diameter.end) / 2
    return Circle(center, radius = radius)
}//можно,конечно, не расписывать такое количество переменных,но лично мне так понятнее


/**
 * Прямая, заданная точкой point и углом наклона angle (в радианах) по отношению к оси X.
 * Уравнение прямой: (y - point.y) * cos(angle) = (x - point.x) * sin(angle)
 * или: y * cos(angle) = x * sin(angle) + b, где b = point.y * cos(angle) - point.x * sin(angle).
 * Угол наклона обязан находиться в диапазоне от 0 (включительно) до PI (исключительно).
 */
class Line private constructor(val b: Double, val angle: Double) {
    init {
        require(angle >= 0 && angle < PI) { "Incorrect line angle: $angle" }
    }

    constructor(point: Point, angle: Double) : this(point.y * cos(angle) - point.x * sin(angle), angle)

    /**
     * Средняя
     *
     * Найти точку пересечения с другой линией.
     * Для этого необходимо составить и решить систему из двух уравнений (каждое для своей прямой)
     */
    fun crossPoint(other: Line): Point {

        val alpha = angle
        val bettha = other.angle
        if (alpha == bettha) throw IllegalArgumentException()
        val crossPointX = (b * cos(bettha) - other.b * cos(alpha)) / sin(bettha - alpha)
        val crossPointY =
                if (alpha == PI / 2) (crossPointX * sin(bettha) + other.b) / cos(bettha)
                else {
                    (crossPointX * sin(alpha) + b) / cos(alpha)
                }

        return Point(crossPointX, crossPointY)
    }

    override fun equals(other: Any?) = other is Line && angle == other.angle && b == other.b

    override fun hashCode(): Int {
        var result = b.hashCode()
        result = 31 * result + angle.hashCode()
        return result
    }

    override fun toString() = "Line(${cos(angle)} * y = ${sin(angle)} * x + $b)"
}

/**
 * Средняя
 *
 * Построить прямую по отрезку
 */
fun lineBySegment(s: Segment): Line = lineByPoints(s.begin, s.end)

/**
 * Средняя
 *
 * Построить прямую по двум точкам
 */
fun lineByPoints(a: Point, b: Point): Line {
    val alpha = (atan((a.y - b.y) / (a.x - b.x)) + 2 * PI) % PI
    return if (alpha >= 0) Line(a, alpha)
    else Line(b, alpha)
}

/**
 * Сложная
 *
 * Построить серединный перпендикуляр по отрезку или по двум точкам
 */
fun bisectorByPoints(a: Point, b: Point): Line { //ну и намучалась же я с этой задачей
    val alpha = (atan((a.y - b.y) / (a.x - b.x)) + PI / 2) % PI
    val center = Point((a.x + b.x) / 2, (a.y + b.y) / 2)
    return when {
        alpha >= PI -> Line(center, alpha - PI)
        alpha >= 0 -> Line(center, alpha)
        else -> (Line(center, alpha + PI))
    }
}

/**
 * Средняя
 *
 * Задан список из n окружностей на плоскости. Найти пару наименее удалённых из них.
 * Если в списке менее двух окружностей, бросить IllegalArgumentException
 */
fun findNearestCirclePair(vararg circles: Circle): Pair<Circle, Circle> = TODO()

/**
 * Сложная
 *
 * Дано три различные точки. Построить окружность, проходящую через них
 * (все три точки должны лежать НА, а не ВНУТРИ, окружности).
 * Описание алгоритмов см. в Интернете
 * (построить окружность по трём точкам, или
 * построить окружность, описанную вокруг треугольника - эквивалентная задача).
 */
fun circleByThreePoints(a: Point, b: Point, c: Point): Circle { //хоть где-то пригадились билеты с зачетов по геометрии с шк...
    val bisLine1 = bisectorByPoints(a, b)
    val bisLine2 = bisectorByPoints(b, c)
    val centerOfCircle = bisLine1.crossPoint(bisLine2)
    return Circle(centerOfCircle, radius = centerOfCircle.distance(b))

}//сначала написала все это через "=", получилось компактно,но не читабельно

/**
 * Очень сложная
 *
 * Дано множество точек на плоскости. Найти круг минимального радиуса,
 * содержащий все эти точки. Если множество пустое, бросить IllegalArgumentException.
 * Если множество содержит одну точку, вернуть круг нулевого радиуса с центром в данной точке.
 *
 * Примечание: в зависимости от ситуации, такая окружность может либо проходить через какие-либо
 * три точки данного множества, либо иметь своим диаметром отрезок,
 * соединяющий две самые удалённые точки в данном множестве.
 */
fun minContainingCircle(vararg points: Point): Circle = TODO()
//я тут вроде как поняла как решать, но объяснить компудактеру как это делать у мменя не особо вышло
// возможно проблема в алгоритме, мне кажется я упускаю какую-то важную деталь
// возможно за ночь я пойму как это делать
// но если нет, то ПОЖАЛУЙСТА подскажите что тут не так
/* 1. чекнуть на пустоту поинты и бросится исключением
   2. если точета 1 = бахнуть кружок с нулевым радиусом //вот тут я долго думала
   3. если точки 2 = бахнуть кружок побольше с радиусом = диаметр/2 (circleByDiameter - хелпанет мне, наверное)
   4. а вот тут начинается танцы до утра с 3 точечками
       а) тут сто-проц нужно circleBy3Points
       б) нужна перемнная для минимального радиуса, чтоб по нему уже искать этот кружок
       в) чтоб найти что-то минимальное, сначало нужно задать что-то максимальное - это я еще с молодости егэшного паскаля помню
       г) собсна, бегаем по трем точечькам с помощью циклов for ну или что-то более оригинальное и кошерное в помощь
       д) наверное, где-то тут должен примчатся на помощь пункт (а), но это не точно
       е) сравнение радиусов
       ж) вывод радиуса
* */


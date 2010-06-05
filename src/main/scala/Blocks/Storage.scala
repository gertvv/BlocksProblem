package Blocks

final class Storage(val columns: List[Column]) {
	def move(box: Box): StorageInFlux = new StorageInFlux(
		applyToMatchingColumns(box, move), new Column(List(box)))
	def pile(box: Box): StorageInFlux = displace(box, pile)

	type Action = (Box, Column) => Column
	type Action2 = (Box, Column) => (Column, Column)

	def applyToMatchingColumns(box: Box, action: Action) =
		new Storage(columns.map(
			c => if (c.contains(box)) action(box, c) else c))

	private def displace(box: Box, action: Action2)
	: StorageInFlux = {
		val result = displace(box, columns, action)
		new StorageInFlux(new Storage(result._1), result._2)
	}

	private def displace(box: Box, l0: List[Column], action: Action2)
	: (List[Column], Column) = l0 match {
		case column :: l1 => if (column.contains(box)) {
				val newColumns = action(box, column)
				(newColumns._1 :: l1, newColumns._2)
			} else {
				prependColumn(displace(box, l1, action), column)
			}
		case Nil => throw new IllegalArgumentException()
	}

	private def prependColumn(displacement: (List[Column], Column), column: Column)
	: (List[Column], Column) = (column :: displacement._1, displacement._2)

	private def pile(box: Box, source: Column): (Column, Column) = {
		val unstacked = unstack(box, source, Column())
		(unstacked._1, unstacked._2.push(box))
	}

	private def move(box: Box, source: Column): Column = {
		val unstacked = unstack(box, source, Column())
		(unstacked._1.pushAll(unstacked._2))
	}

	/**
	 * Unstacks elements until it encounters box. Returns a tuple with the elements remaining on the stack
	 * and elements taken from the stack (excluding box)
	 */
	private def unstack(box: Box, source: Column, target: Column): (Column, Column) = if (source.isEmpty) {
		throw new IllegalStateException()
	} else {
		if (box == source.top) (source.pop, target)
		else unstack(box, source.pop, target.push(source.top))
	}

	override def equals(o: Any) = o match {
		case other: Storage => other.columns == columns
		case _ => false
	}

	override def hashCode = columns.hashCode
}

object Storage {
	def apply(args: Column*): Storage = {
		new Storage(args.toList)
	}

	def apply(boxes: List[String]): Storage = {
		new Storage(boxes.map(b => Column(b)))
	}
}

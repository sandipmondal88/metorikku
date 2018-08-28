package com.yotpo.metorikku.output.writers.parquet

import com.yotpo.metorikku.configuration.outputs.File
import com.yotpo.metorikku.output.MetricOutputWriter
import org.apache.log4j.LogManager
import org.apache.spark.sql.{DataFrame, SaveMode}


class ParquetOutputWriter(props: Map[String, String], outputFile: Option[File]) extends MetricOutputWriter {
  val NO_REPARTITION = 0
  case class ParquetOutputProperties(saveMode: SaveMode, path: String, partitionBy: Seq[String])

  val log = LogManager.getLogger(this.getClass)
  val partitionBy = props.getOrElse("partitionBy",Seq.empty).asInstanceOf[Seq[String]]
  val repartitionValue = props.getOrElse("repartition",NO_REPARTITION).asInstanceOf[Integer]
  val parquetOutputOptions = ParquetOutputProperties(SaveMode.valueOf(props("saveMode")),
                                                     props("path"),
                                                     partitionBy)

  override def write(dataFrame: DataFrame): Unit = {
    outputFile match {
      case Some(outputFile) =>
        val outputPath = outputFile.dir + "/" + parquetOutputOptions.path
        log.info(s"Writing Parquet Dataframe to ${outputPath}")

        var writer = if(repartitionValue == NO_REPARTITION) dataFrame.write else dataFrame.repartition(repartitionValue).write
        if (parquetOutputOptions.partitionBy.nonEmpty) {
          writer = writer.partitionBy(parquetOutputOptions.partitionBy: _*)
        }
        writer.mode(parquetOutputOptions.saveMode).parquet(outputPath)

      case None => log.error(s"Parquet file configuration were not provided")
    }

  }
}


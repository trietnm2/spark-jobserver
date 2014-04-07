package spark.jobserver

import java.io.{BufferedOutputStream, FileOutputStream}
import spark.jobserver.io.{JobDAO, JobInfo}
import org.joda.time.DateTime
import scala.collection.mutable
import com.typesafe.config.Config

/**
 * In-memory DAO for easy unit testing
 */
class InMemoryDAO extends JobDAO {
  val jars = mutable.HashMap.empty[(String, DateTime), Array[Byte]]

  def saveJar(appName: String, uploadTime: DateTime, jarBytes: Array[Byte]) {
    jars((appName, uploadTime)) = jarBytes
  }

  def getApps(): Map[String, Seq[DateTime]] = {
    jars.keys
      .groupBy(_._1)
      .map { case (appName, appUploadTimeTuples) =>
        appName -> appUploadTimeTuples.map(_._2).toSeq
      }.toMap
  }

  def retrieveJarFile(appName: String, uploadTime: DateTime): String = {
    // Write the jar bytes to a temporary file
    val outFile = java.io.File.createTempFile("InMemoryDAO", ".jar")
    outFile.deleteOnExit()
    val bos = new BufferedOutputStream(new FileOutputStream(outFile))
    try {
      bos.write(jars((appName, uploadTime)))
    } finally {
      bos.close()
    }
    outFile.getAbsolutePath()
  }

  val jobInfos = mutable.HashMap.empty[String, JobInfo]

  def saveJobInfo(jobInfo: JobInfo) { jobInfos(jobInfo.jobId) = jobInfo }

  def getJobInfos(): Map[String, JobInfo] = jobInfos.toMap

  def saveJobConfig(jobConfig: Config, jobInfo: JobInfo) {}
}

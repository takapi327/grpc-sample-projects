
import com.typesafe.sbt.packager.docker.*

object DockerCommands {

  val grpcCurl: Seq[CmdLike] = Seq(
    ExecCmd(
      "RUN",
      "yum",
      "-y",
      "install",
      "go"
    ),
    ExecCmd(
      "RUN",
      "go",
      "install",
      "github.com/fullstorydev/grpcurl/cmd/grpcurl"
    )
  )

  val grpcHealthProbe: Seq[ExecCmd] = Seq(
    ExecCmd(
      "RUN",
      "yum",
      "-y",
      "install",
      "wget"
    ),
    ExecCmd(
      "RUN",
      "wget",
      "-q",
      "-O",
      "/bin/grpc_health_probe",
      "https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/v0.3.1/grpc_health_probe-linux-amd64"
    ),
    ExecCmd(
      "RUN",
      "chmod",
      "+x",
      "/bin/grpc_health_probe"
    )
  )
}

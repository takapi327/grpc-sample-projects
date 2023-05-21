
import com.typesafe.sbt.packager.docker.*

object DockerCommands {

  val grpcCurl: Seq[CmdLike] = Seq(
    Cmd("ARG", "version=11.0.19.7-1"),
    ExecCmd(
      "RUN",
      "set",
      "-eux",
      "&&",
      "rpm -import file:///etc/pki/rpm-gpg/RPM-GPG-KEY-amazon-linux-2023",
      "&&",
      "echo localpkg_gpgcheck=1 >> /etc/dnf/dnf.conf",
      "&&",
      "CORRETO_TEMP=$(mktemp -d)",
      "&&",
      "pushd ${CORRETO_TEMP}",
      "&&",
      "RPM_LIST=(\"java-11-amazon-corretto-headless-$version.amzn2023.$(uname -m).rpm\" \"java-11-amazon-corretto-$version.amzn2023.$(uname -m).rpm\" \"java-11-amazon-corretto-devel-$version.amzn2023.$(uname -m).rpm\" \"java-11-amazon-corretto-jmods-$version.amzn2023.$(uname -m).rpm\")",
      "&&",
      "for rpm in ${RPM_LIST[@]}; do curl --fail -O https://corretto.aws/downloads/resources/$(echo $version | tr '-' '.')/${rpm}",
      "&&",
      "rpm -K \"${CORRETO_TEMP}/${rpm}\" | grep -F \"${CORRETO_TEMP}/${rpm}: digests signatures OK\" || exit 1; done",
      "&&",
      "dnf install -y ${CORRETO_TEMP}/*.rpm",
      "&&",
      "popd",
      "&&",
      "rm -rf /usr/lib/jvm/java-11-amazon-corretto.$(uname -m)/lib/src.zip",
      "&&",
      "rm -rf ${CORRETO_TEMP}",
      "&&",
      "dnf clean all",
      "&&",
      "sed -i '/localpkg_gpgcheck=1/d' /etc/dnf/dnf.conf",
    ),
    Cmd("ENV", "LANG", "C.UTF-8"),
    Cmd("ENV", "JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto"),
    ExecCmd("RUN", "/bin/bash", "-c", "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"),
    ExecCmd("RUN", "brew", "install", "grpcurl")
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

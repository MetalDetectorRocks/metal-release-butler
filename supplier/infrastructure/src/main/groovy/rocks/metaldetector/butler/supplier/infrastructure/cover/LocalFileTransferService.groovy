package rocks.metaldetector.butler.supplier.infrastructure.cover

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel

@Service
@Profile(["default"])
class LocalFileTransferService {

  long transferFileFromUrl(URL source, String targetPath) {
    ReadableByteChannel readableByteChannel = Channels.newChannel(source.openStream())
    FileOutputStream fileOutputStream = new FileOutputStream(targetPath)
    FileChannel fileChannel = fileOutputStream.getChannel()
    return fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
  }
}

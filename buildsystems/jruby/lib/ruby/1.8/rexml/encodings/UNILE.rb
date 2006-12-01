#
# All content copyright (c) 2003-2006 Terracotta, Inc.,
# except as may otherwise be noted in a separate copyright notice.
# All rights reserved
#

module REXML
  module Encoding
    def encode_unile content
      array_utf8 = content.unpack("U*")
      array_enc = []
      array_utf8.each do |num|
        if ((num>>16) > 0)
          array_enc << ??
          array_enc << 0
        else
          array_enc << (num & 0xFF)
          array_enc << (num >> 8)
        end
      end
      array_enc.pack('C*')
    end

    def decode_unile(str)
      array_enc=str.unpack('C*')
      array_utf8 = []
      2.step(array_enc.size-1, 2){|i| 
        array_utf8 << (array_enc.at(i) + array_enc.at(i+1)*0x100)
      }
      array_utf8.pack('U*')
    end

    register(UNILE) do |obj|
      class << obj
        alias decode decode_unile
        alias encode encode_unile
      end
    end
  end
end

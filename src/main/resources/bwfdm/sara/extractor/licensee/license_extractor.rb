require "licensee"
require "java"

module LicenseExtractor extend self
  # implements licensee's filesystem access abstraction by just
  # delegating to Java
  class JavaProject < Licensee::Project
    def initialize(files)
      @files = files
    end

    def files
      @files.map do |file|
        { name: file.name, handle: file }
      end
    end

    def load_file(file)
      file[:handle].content
    end
  end

  # data class implementation of bwfdm.sara.license.LicenseFile
  class LicenseFile
    include Java::BwfdmSaraExtractor::LicenseFile

    attr_reader :file, :id, :name, :score

    def initialize(file)
      @file = file.filename
      @id = file.license.meta['spdx-id']
      @name = file.license.meta['title']
      @score = file.confidence / 100
    end
  end

  # main entry point called from Java
  def detect_license(files)
    file = JavaProject.new(files).matched_file
    return nil unless file
    LicenseFile.new(file)
  end
end

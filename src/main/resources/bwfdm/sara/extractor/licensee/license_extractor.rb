require "licensee"
require "java"

module LicenseExtractor extend self
  # implements licensee's filesystem access abstraction by just
  # delegating to Java
  class JavaProject < Licensee::Project
    def initialize(repo, files)
      @repo = repo
      @files = files
    end

    def files
      @files.map do |file|
        { name: file.name, hash: file.hash }
      end
    end

    def load_file(file)
      @repo.read_string file[:hash]
    end
  end

  # data class implementation of LicenseeFile, to conveniently pass
  # everything to Java in a structured way
  class LicenseeFile
    include Java::BwfdmSaraExtractorLicensee::LicenseeFile

    attr_reader :file, :id, :name, :score

    def initialize(file)
      @file = file.filename
      @id = file.license.meta['spdx-id']
      @name = file.license.meta['title']
      @score = file.confidence / 100
    end
  end

  # main entry point called from Java
  def detect_license(repo, files)
    file = JavaProject.new(repo, files).matched_file
    return nil unless file
    LicenseeFile.new(file)
  end
end

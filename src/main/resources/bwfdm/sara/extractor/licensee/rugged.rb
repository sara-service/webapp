# this provides just enough of Rugged to fulfill the Rugged::Repository
# import in Licensee::GitProject.
# actually trying to use it makes Licensee::GitProject.new() throw an
# error, so that Licensee.project() falls back to using a FileProject
# (which, unlike Rugged, works in JRuby).
module Rugged
	class OSError < ArgumentError; end
	class RepositoryError < ArgumentError; end

	class Repository
		def initialize(path, **args)
			raise OSError, "this is not a very rugged implementation"
		end
	end
end

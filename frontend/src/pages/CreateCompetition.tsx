import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { competitionApi } from '../services/api';
import type { CompetitionFormat, TeamSize } from '../types';
import Header from '../components/layout/Header';

export default function CreateCompetition() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Competition fields
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [format, setFormat] = useState<CompetitionFormat>('LEAGUE');
  const [teamSize, setTeamSize] = useState<TeamSize>('ELEVEN_V_ELEVEN');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [registrationDeadline, setRegistrationDeadline] = useState('');
  const [entryFee, setEntryFee] = useState('');
  const [maxTeams, setMaxTeams] = useState('');

  // Venue fields
  const [venueName, setVenueName] = useState('');
  const [address, setAddress] = useState('');

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const competition = await competitionApi.create({
        name,
        description,
        format,
        teamSize,
        startDate,
        endDate,
        registrationDeadline,
        entryFee: parseFloat(entryFee),
        maxTeams: parseInt(maxTeams),
        venue: {
          name: venueName,
          address,
        },
      });
      navigate(`/competitions/${competition.id}`);
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.errors
          ? Object.entries(err.response.data.errors)
              .map(([field, msg]) => `${field}: ${msg}`)
              .join(', ')
          : err.response?.data?.message || 'Failed to create competition';
      setError(errorMessage as string);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="max-w-3xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            Create New Competition
          </h2>

          {error && (
            <div className="rounded-md bg-red-50 p-4 mb-6">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-8">
            {/* Competition Details */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium text-gray-900">
                Competition Details
              </h3>
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                  Competition Name *
                </label>
                <input
                  id="name"
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
              </div>

              <div>
                <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                  Description *
                </label>
                <textarea
                  id="description"
                  required
                  rows={4}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="format" className="block text-sm font-medium text-gray-700">
                    Format *
                  </label>
                  <select
                    id="format"
                    value={format}
                    onChange={(e) => setFormat(e.target.value as CompetitionFormat)}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  >
                    <option value="LEAGUE">League</option>
                    <option value="KNOCKOUT">Knockout</option>
                    <option value="ROUND_ROBIN">Round Robin</option>
                  </select>
                </div>

                <div>
                  <label htmlFor="teamSize" className="block text-sm font-medium text-gray-700">
                    Team Size *
                  </label>
                  <select
                    id="teamSize"
                    value={teamSize}
                    onChange={(e) => setTeamSize(e.target.value as TeamSize)}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  >
                    <option value="FIVE_V_FIVE">5v5</option>
                    <option value="SIX_V_SIX">6v6</option>
                    <option value="SEVEN_V_SEVEN">7v7</option>
                    <option value="EIGHT_V_EIGHT">8v8</option>
                    <option value="NINE_V_NINE">9v9</option>
                    <option value="ELEVEN_V_ELEVEN">11v11</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label htmlFor="startDate" className="block text-sm font-medium text-gray-700">
                    Start Date *
                  </label>
                  <input
                    id="startDate"
                    type="date"
                    required
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  />
                </div>

                <div>
                  <label htmlFor="endDate" className="block text-sm font-medium text-gray-700">
                    End Date *
                  </label>
                  <input
                    id="endDate"
                    type="date"
                    required
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  />
                </div>

                <div>
                  <label htmlFor="registrationDeadline" className="block text-sm font-medium text-gray-700">
                    Registration Deadline *
                  </label>
                  <input
                    id="registrationDeadline"
                    type="date"
                    required
                    value={registrationDeadline}
                    onChange={(e) => setRegistrationDeadline(e.target.value)}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="entryFee" className="block text-sm font-medium text-gray-700">
                    Entry Fee ($) *
                  </label>
                  <input
                    id="entryFee"
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    value={entryFee}
                    onChange={(e) => setEntryFee(e.target.value)}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  />
                </div>

                <div>
                  <label htmlFor="maxTeams" className="block text-sm font-medium text-gray-700">
                    Max Teams *
                  </label>
                  <input
                    id="maxTeams"
                    type="number"
                    min="2"
                    required
                    value={maxTeams}
                    onChange={(e) => setMaxTeams(e.target.value)}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  />
                </div>
              </div>
            </div>

            {/* Venue Details */}
            <div className="space-y-4">
              <h3 className="text-lg font-medium text-gray-900">Venue Details</h3>
              <div>
                <label htmlFor="venueName" className="block text-sm font-medium text-gray-700">
                  Venue Name *
                </label>
                <input
                  id="venueName"
                  type="text"
                  required
                  value={venueName}
                  onChange={(e) => setVenueName(e.target.value)}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
              </div>

              <div>
                <label htmlFor="address" className="block text-sm font-medium text-gray-700">
                  Address *
                </label>
                <textarea
                  id="address"
                  rows={2}
                  required
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  placeholder="Full address including city, state, and ZIP code"
                />
              </div>
            </div>

            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => navigate('/competitions')}
                className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {loading ? 'Creating...' : 'Create Competition'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
